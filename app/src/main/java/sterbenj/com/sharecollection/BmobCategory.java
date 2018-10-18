package sterbenj.com.sharecollection;

import cn.bmob.v3.BmobObject;

public class BmobCategory extends BmobObject {
    private String Title;
    private Byte[] Icon;
    private String PackageName;
    private String Context;
    private String UserId;

    public String getTitle() {
        return Title;
    }

    public Byte[] getIcon() {
        return Icon;
    }

    public String getPackageName() {
        return PackageName;
    }

    public String getContext() {
        return Context;
    }

    public String getUserId() {
        return UserId;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setIcon(Byte[] icon) {
        Icon = icon;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public void setContext(String context) {
        Context = context;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
