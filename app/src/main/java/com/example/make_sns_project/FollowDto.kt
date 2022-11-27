package com.example.make_sns_project

data class FollowDto(
    // 이 사람을 팔로잉하는 사람들
    var followers: MutableMap<String, Boolean> = HashMap(),
    // 이 사람이 팔로잉 중인 사람들
    var followings: MutableMap<String, Boolean> = HashMap()
)