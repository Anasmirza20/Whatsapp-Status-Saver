package com.whatsappstatus.util

import android.os.Environment


object Constants {

    const val TITLE: String = "TITLE"
    const val WEB_URL: String = "WEB_URL"
    var IS_CONNECTED = false
    const val REELSFIZ = "REELS_FIZ"

    //    const val BASE_URL = "http://reelsfiz-env.eba-dwd37wdm.ap-south-1.elasticbeanstalk.com/"
//    const val BASE_URL = "http://reelsfiz-env.eba-2gvs8sav.ap-south-1.elasticbeanstalk.com/"
    const val BASE_URL = "http://reelsfiz-db.ap-south-1.elasticbeanstalk.com/"
//    const val BASE_URL = "http://192.168.0.177:5000/"

    //SharedPrefKeys
    const val SHARED_PREF_KEY = "com.reelsfiz.SharedPrefKey"
    const val STATUS_URI_KEY = "com.reelsfiz.STATUS_URI"
    const val USER_NAME_PREF_KEY = "com.reelsfiz.USER_NAME"
    const val USER_ID_PREF_KEY = "com.reelsfiz.USER_ID"
    const val REEL_ID_PREF_KEY = "com.reelsfiz.REEL_ID"
    const val REEL_NAME = "REEL_NAME"
    const val REEL_ID = "REEL_ID"
    const val MOBILE_NO = "MOBILE_NO"
    const val USER_PROFILE_IMAGE_PREF_KEY = "com.reelsfiz.USER_PROFILE_IMAGE"
    const val STATUS_PERMISSION_GRANTED_KEY = "com.reelsfiz.STATUS_PERMISSION_GRANTED_KEY"
    const val IS_LOGGED_IN_KEY = "com.reelsfiz.IS_LOGGED_IN_KEY"


    const val user_id = "userId"
    const val PAGE_NO = "pageNo"
    const val PAGE_SIZE = "pageSize"
    const val REEL_PAGE_SIZE = 5
    const val category_id = "categoryId"
    const val reel_id = "reelId"
    const val EMAIL = "email"
    const val MOBILE = "mobile"


    const val REELS_COLLECTION_NAME = "Reels"
    const val USER_COLLECTION_NAME = "Users"
    const val COMMENT_COLLECTION_NAME = "Comments"
    const val LIKES_COLLECTION_NAME = "Likes"
    const val CATEGORIES_COLLECTION_NAME = "Categories"
    const val REEL_URI = "reel_uri"
    const val VIDEO_URI = "video_uri"
    const val REEL_MODEL = "reel_model"
    const val CATEGORY_ID = "categoryId"
    const val TIME_STAMP = "timeStamp"
    const val ID = "id"
    const val REELFIZ_VIDEOS_PATH = "/Reelsfiz%20videos"


    var MAX_FILE_SIZE = (80 * 1024 * 1024).toLong()

    var MIN_REEL_DURATION = (3 * 1000).toLong()
    var MAX_REEL_DURATION = (31 * 1000).toLong()
    var AD_MIN_CLICK_DURATION: Long = 3000


    // FileParams
    const val KEY_FILE_URL = "key_file_url"
    const val KEY_FILE_TYPE = "key_file_type"
    const val KEY_FILE_NAME = "key_file_name"
    const val KEY_FILE_URI = "key_file_uri"

    // NotificationConstants
    const val CHANNEL_NAME = "download_file_worker_demo_channel"
    const val CHANNEL_DESCRIPTION = "download_file_worker_demo_description"
    const val CHANNEL_ID = "download_file_worker_demo_channel_123456"
    const val NOTIFICATION_ID = 1


    val REELS_DIRECTORY = Environment.DIRECTORY_DOWNLOADS + "/Reelsfiz videos/"

    val IMAGES_DIRECTORY = Environment.DIRECTORY_PICTURES + "/Reelsfiz images/"

/*
    val listOfVideos = listOf(
        "https://firebasestorage.googleapis.com/v0/b/reelsfiz.appspot.com/o/videos%2FJab%20Tak%20hai%20jaan%204.mp4?alt=media&token=bfafc80e-e70c-4ff1-a45d-78647b9d05c5",
        "https://firebasestorage.googleapis.com/v0/b/reelsfiz.appspot.com/o/videos%2F279003928_1205084513362672_5227171126356107810_n.mp4?alt=media&token=4e070e6b-c6a5-46b0-b8bc-ae051f8343cc",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728783725-279747250_286069530403942_5376085365549924771_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728723338-280184944_398397492163612_5495752439291341609_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728696984-278504838_2077863392369990_4576234467717221839_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728667855-278023291_5240136482698154_6749200864774917386_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728632850-277477096_1622145531476075_6313762253886758206_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728601927-277955257_1639044266481733_8495821376603666784_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728563137-277429833_352071926940976_4419343596917122914_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728533558-278570181_1596416884091094_2419632206173115488_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728505466-278739715_933319140670819_3187258373400861902_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728461950-278036083_515310436716927_4125361520187808449_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728431540-10000000_512339257141866_1760902296069667347_n.mp4",
        "https://pando2.s3.ap-southeast-1.amazonaws.com/1653728208222-279003928_1205084513362672_5227171126356107810_n.mp4",
    )*/
}