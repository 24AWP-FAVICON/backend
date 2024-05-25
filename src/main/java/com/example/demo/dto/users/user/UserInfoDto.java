package com.example.demo.dto.users.user;

import com.example.demo.entity.community.block.Block;
import com.example.demo.entity.community.follow.Follow;
import com.example.demo.entity.community.post.Post;
import com.example.demo.entity.planner.Trip;
import com.example.demo.entity.users.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoDto {

    private String userId;
    private String nickname;
    private List<Trip> trips;
    private List<Post> postList;
    private List<Follow> followerList;
    private List<Follow> followingList;
    private List<Block> blockList;

    public static UserInfoDto toDto(User user){
        return new UserInfoDto(
          user.getUserId(),
          user.getNickname(),
          user.getTripList(),
          user.getPostList(),
          user.getFollowerList(),
          user.getFollowingList(),
          user.getBlockList()
        );
    }

}

