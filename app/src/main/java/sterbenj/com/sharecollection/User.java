package sterbenj.com.sharecollection;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
    private Byte[] icon;

    public Byte[] getIcon() {
        return icon;
    }

    public void setIcon(Byte[] icon) {
        this.icon = icon;
    }
}
