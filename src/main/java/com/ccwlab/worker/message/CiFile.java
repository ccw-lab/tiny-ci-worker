package com.ccwlab.worker.message;

import java.util.List;

public class CiFile {
    String image;
    List<String> cmd;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getCmd() {
        return cmd;
    }

    public void setCmd(List<String> cmd) {
        this.cmd = cmd;
    }
}
