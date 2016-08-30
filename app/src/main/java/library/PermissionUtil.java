package library;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by hzlinxuanxuan on 2016/8/30.
 */
public class PermissionUtil {
    public static final int PERMISSION_REQUEST_CODE = 10001;

    public static synchronized long startRequest(OnPermissionCallback callback, Activity actv, BaseFragment frag, long lastRequestPermissionsTime,
                                                 ArrayList<RequestWithPermission> undoRequests, RequestWithPermission... requests) {
        //如果发现上次权限回来之前与现在距离不到100毫秒的话，则认为是短时间内重复，忽略本次请求
        if (System.currentTimeMillis() - lastRequestPermissionsTime <= 100) {
            callback.onAllPermissionRejected();
            return System.currentTimeMillis();
        }
        //若正在请求中，则记录当前的参数，稍后等该次权限申请结束后再次调用
        undoRequests.clear();
        ArrayList<String> tempPermissions;
        //防止不同请求会有相同的权限，使用Set
        Set<String> needRequestPermissions = new HashSet<>();
        for (RequestWithPermission request : requests) {
            tempPermissions = request.getRequestPermissions(actv);
            if (tempPermissions == null) {
                request.execute();
                continue;
            }
            needRequestPermissions.addAll(tempPermissions);
            undoRequests.add(request);
        }
        if (needRequestPermissions.size() != 0) {
            if (frag != null) {
                frag.requestPermissions(needRequestPermissions.toArray(new String[]{}), PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(actv, needRequestPermissions.toArray(new String[]{}), PERMISSION_REQUEST_CODE);
            }
        }
        return lastRequestPermissionsTime;
    }



    public static void onRequestPermissionsResult(ArrayList<RequestWithPermission> requests, String[] permissions, int[] grantResults, OnPermissionCallback callback) {
        //首先选出被赋予的权限
        ArrayList<String> grantedPermissions = getGrantedPermissions(permissions, grantResults);
        ArrayList<RequestWithPermission.ITransactionListener> todoTask = new ArrayList<>();
        ArrayList<RequestWithPermission.ITransactionListener> undoTask = new ArrayList<>();
        Set<String> ungrantedPermissions = new HashSet<>();
        ArrayList<String> temp;
        for (int i = 0; i < requests.size(); i++) {
            temp = requests.get(i).getUngrantedNecessaryPermission(grantedPermissions);
            if (temp.size() == 0) {
                todoTask.add(requests.get(i).task);
            } else {
                undoTask.add(requests.get(i).task);
                ungrantedPermissions.addAll(temp);
            }
        }
        notifyShowToast(ungrantedPermissions);
        if (todoTask.size() != 0) {
            callback.onPermissionRequestSucc(todoTask);
        } else {
            callback.onAllPermissionRejected();
        }
        if (undoTask.size() != 0) {
            callback.onPermissionRequestFail(undoTask);
        }
    }

    public static ArrayList<String> getGrantedPermissions(String[] permissions, int[] grantResults) {
        ArrayList<String> grantedPermissions = new ArrayList<>();
        for (int i = 0; permissions != null && i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            }
        }
        return grantedPermissions;
    }

    //将权限归组，根据组来提示toast。
    private static void notifyShowToast(Set<String> ungrantedNecessaryPermissions) {
        Set<String> groups = new HashSet<>();
        Iterator<String> iterator = ungrantedNecessaryPermissions.iterator();
        while (iterator.hasNext()) {
            switch (iterator.next()) {
                case Manifest.permission.READ_CALENDAR:
                case Manifest.permission.WRITE_CALENDAR:
                    groups.add("日历");
                    break;
                case Manifest.permission.CAMERA:
                    groups.add("相机");
                    break;
                case Manifest.permission.READ_CONTACTS:
                case Manifest.permission.WRITE_CONTACTS:
                case Manifest.permission.GET_ACCOUNTS:
                    groups.add("通讯录");
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    groups.add("位置信息");
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    groups.add("麦克风");
                    break;
                case Manifest.permission.READ_PHONE_STATE:
                case Manifest.permission.CALL_PHONE:
                case Manifest.permission.READ_CALL_LOG:
                case Manifest.permission.WRITE_CALL_LOG:
                case Manifest.permission.ADD_VOICEMAIL:
                case Manifest.permission.USE_SIP:
                case Manifest.permission.PROCESS_OUTGOING_CALLS:
                    groups.add("电话");
                    break;
                case Manifest.permission.BODY_SENSORS:
                    groups.add("传感器");
                    break;
                case Manifest.permission.SEND_SMS:
                case Manifest.permission.RECEIVE_SMS:
                case Manifest.permission.READ_SMS:
                case Manifest.permission.RECEIVE_WAP_PUSH:
                case Manifest.permission.RECEIVE_MMS:
                    groups.add("短信");
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    groups.add("存储空间");
                    break;
            }
        }
        StringBuilder content = new StringBuilder();
        iterator = groups.iterator();
        if(iterator.hasNext()){
            content.append(iterator.next());
        }
        while(iterator.hasNext()){
            content.append("、" + iterator.next());
        }
        if (!TextUtils.isEmpty(content)) {
            Log.d("tag","未授予" + content + "相关权限，请授予后再继续操作");
        }
    }

    public interface OnPermissionCallback {
        void onPermissionRequestSucc(ArrayList<RequestWithPermission.ITransactionListener> tasks);

        void onPermissionRequestFail(ArrayList<RequestWithPermission.ITransactionListener> tasks);

        void onAllPermissionRejected();
    }
}
