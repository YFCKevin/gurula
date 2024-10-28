package com.yfckevin.badminton.service;


import com.yfckevin.badminton.dto.RequestPostDTO;
import com.yfckevin.badminton.entity.Post;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface PostService {

    int dataCleaning(String filePath) throws IOException;
    String getDifferencePostsAndSaveInGeneralFileAndReturnFilePath(List<RequestPostDTO> dailyPosts) throws IOException, InterruptedException;
    List<Post> findPostByConditions(String keyword, String startDate, String endDate) throws ParseException;
    List<Post> getPassPostsByLeadersAndTodayBefore();

    void save(Post post);

    Optional<Post> findById(String id);

    List<Post> findTodayNewPosts(String startOfToday, String endOfToday);

    List<Post> findSamePosts();

    void deleteById(String id);

    List<Post> getPostsForToday();

    void deleteByIdIn(List<String> postIdIn);

    List<Post> findPostsByDaySorted(String day, String targetDate);

    List<Post> findByIdIn(List<String> strings);

    void saveAll(List<Post> postList);
}
