package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.FocusUser;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SmallVideoService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer webServer;

    @Autowired
    private RedisTemplate redisTemplate;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    //发布视频
    @CacheEvict(value="videoList",allEntries = true)
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        //1、封面图上传到阿里云OSS，获取地址
        String uploadPath = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //2、视频上传到fdfs上，获取请求地址
        String filename = videoFile.getOriginalFilename();
        String sufix = filename.substring(filename.lastIndexOf(".") + 1);//后缀
        StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), sufix, null);
        String videoUrl = webServer.getWebServerUrl() + storePath.getFullPath();
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());
        video.setPicUrl(uploadPath);
        video.setVideoUrl(videoUrl);
        video.setText("i am gold");
        videoApi.save(video);
    }

    @Cacheable(value="videoList",key="#page + '_' +  #pagesize")
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        //1、调用API查询分页数据 PageResult<Video>
        PageResult result = videoApi.findAll(page,pagesize);
        //2、获取分页对象中list集合  List<Video>
        List<Video> items = (List<Video>) result.getItems();
        //3、一个Video转化成一个VideoVo对象
        List<Long> userIds = CollUtil.getFieldValues(items, "userId",Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<VideoVo> vos = new ArrayList<>();
        for (Video video : items) {
            UserInfo userInfo = map.get(video.getUserId());
            if (userInfo != null){
                VideoVo vo = VideoVo.init(userInfo, video);
                //加入了作者关注功能，从redis判断是否存在关注的key，如果存在设置hasFocus=1
                //code
                vos.add(vo);
            }
        }
        result.setItems(vos);
        return result;
    }

    public void userFocus(Long followUserId) {
        //1、创建FocusUser对象，并设置属性
        FocusUser focusUser = new FocusUser();
        focusUser.setUserId(UserHolder.getUserId());
        focusUser.setFollowUserId(followUserId);
        //2、调用API保存
        videoApi.saveFocusUser(focusUser);
        //3、将关注记录存入redis中
        String key = Constants.FOCUS_USER_KEY + UserHolder.getUserId();
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().put(key, hashKey, "1");
    }

    public void userUnFocus(Long followUserId) {
        videoApi.deleteFocusUser(UserHolder.getUserId(),followUserId);
        String key = Constants.FOCUS_USER_KEY + UserHolder.getUserId();
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().delete(key, hashKey);
    }
}
