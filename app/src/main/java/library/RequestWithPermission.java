package library;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzlinxuanxuan on 2016/8/30.
 */
public class RequestWithPermission {

    public ITransactionListener task;
    public String[] necessarayPermisions;
    public String[] unnecessaryPermissionns;

    public RequestWithPermission(ITransactionListener task, String[] necessaryPermissions) {
        this(task, necessaryPermissions, null);
    }

    public RequestWithPermission(ITransactionListener task, String[] necessaryPermissions, String[] unnecessaryPermissions) {
        this.task = task;
        this.necessarayPermisions = necessaryPermissions;
        this.unnecessaryPermissionns = unnecessaryPermissions;
    }

    public ArrayList<String> getRequestPermissions(Context ctx) {
        if ((necessarayPermisions == null || necessarayPermisions.length == 0)
                && (unnecessaryPermissionns == null || unnecessaryPermissionns.length == 0)) {
            return null;
        }
        return checkPermissiosGranted(ctx);
    }

    //请求权限，如果返回true，则表示所有权限都已被赋予，返回false，则重写函数
    public ArrayList<String> checkPermissiosGranted(Context ctx) {
        ArrayList<String> ungrantedPermission = new ArrayList<>();
        List<String> tempNecessary = toArrayList();
        int length1 = tempNecessary.size();
        int length2 = unnecessaryPermissionns != null ? unnecessaryPermissionns.length : 0;
        String temp;
        for (int i = 0; i < length1 + length2; i++) {
            temp = i < length1 ? necessarayPermisions[i] : unnecessaryPermissionns[i - length1];
            if (ActivityCompat.checkSelfPermission(ctx, temp) != PackageManager.PERMISSION_GRANTED) {
                ungrantedPermission.add(temp);
            } else if (i < length1) {
                tempNecessary.remove(temp);
            }
        }
        necessarayPermisions = tempNecessary.toArray(new String[]{});
        return ungrantedPermission.size() == 0 ? null : ungrantedPermission;
    }

    public ArrayList<String> getUngrantedNecessaryPermission(ArrayList<String> permissions) {
        ArrayList<String> ungrantedPermissions = new ArrayList<>();
        if (necessarayPermisions == null) {
            return ungrantedPermissions;
        }
        for (int i = 0; i < necessarayPermisions.length; i++) {
            if (!permissions.contains(necessarayPermisions[i])) {
                ungrantedPermissions.add(necessarayPermisions[i]);
            }
        }
        return ungrantedPermissions;
    }

    public void execute() {
        if (task != null) {
            task.todo();
        }
    }

    ArrayList<String> toArrayList() {
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; necessarayPermisions != null && i < necessarayPermisions.length; i++) {
            temp.add(necessarayPermisions[i]);
        }
        return temp;
    }

    public interface ITransactionListener {
        void todo();
    }
}
