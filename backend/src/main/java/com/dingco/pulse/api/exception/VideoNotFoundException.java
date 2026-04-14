package com.dingco.pulse.api.exception;

public class VideoNotFoundException extends RuntimeException {

    public VideoNotFoundException(String videoId) {
        super("해당 영상을 찾을 수 없습니다: " + videoId);
    }
}
