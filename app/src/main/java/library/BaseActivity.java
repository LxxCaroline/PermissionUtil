package library;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by hzlinxuanxuan on 2016/8/30.
 */
public class BaseActivity extends AppCompatActivity implements PermissionUtil.OnPermissionCallback{

    ArrayList<RequestWithPermission> undoRequests = new ArrayList<>();
    private long lastRequestPermissionsTime = 0;
    //把所有的task都遍历一遍，获得所有要权限申请的task和permission，统一申请所有权限，权限申请完毕后，统一执行task
    public void startRequest(RequestWithPermission...requests){
        lastRequestPermissionsTime = PermissionUtil.startRequest(this, this, null, lastRequestPermissionsTime, undoRequests, requests);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(undoRequests, permissions, grantResults, this);
        lastRequestPermissionsTime = System.currentTimeMillis();
    }

    @Override
    public void onPermissionRequestSucc(ArrayList<RequestWithPermission.ITransactionListener> tasks) {
        if (tasks != null) {
            for (RequestWithPermission.ITransactionListener task : tasks) {
                task.todo();
            }
        }
    }

    @Override
    public void onPermissionRequestFail(ArrayList<RequestWithPermission.ITransactionListener> tasks) {
    }

    @Override
    public void onAllPermissionRejected() {
    }
}
