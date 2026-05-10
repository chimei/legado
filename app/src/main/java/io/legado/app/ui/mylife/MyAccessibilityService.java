package io.legado.app.ui.mylife;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Base64;

import io.legado.app.ui.book.read.ReadBookActivity;

@SuppressLint("AccessibilityPolicy")
public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyTag";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.i(TAG, "onAccessibilityEvent: event:" + event.getEventType()  + " ReadBookActivity.Companion.getAUTO_INPUT_AMD:" + ReadBookActivity.Companion.getAUTO_INPUT_AMD());
        if(ReadBookActivity.Companion.getAUTO_INPUT_AMD() == 1){
//            Log.i(TAG, "onAccessibilityEvent: event:" + event.getEventType() + " 写入用户名");
            String username = base64ToString("5pWP5Li75Lq655qE5bCP54uX");
            boolean res = autoInputText(event,username);
            return;
        }
        if(ReadBookActivity.Companion.getAUTO_INPUT_AMD() == 2){
            boolean res = autoInputText(event,"1928374650");
        }
    }

    public static String base64ToString(String base64Str) {
        try {
            byte[] decodedBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decodedBytes = Base64.getDecoder().decode(base64Str);
            }
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean autoInputText(AccessibilityEvent event, String text) {
        // 获取当前焦点的输入框
        AccessibilityNodeInfo focusNode = event.getSource();
        if (focusNode == null) return false;

        // 判断：是不是可输入的框
        if (focusNode.isEditable() && focusNode.isEnabled()) {
            // 执行自动输入（你想输什么改这里）
            InputText(focusNode, text);
            // 用完回收
            focusNode.recycle();
            return true;
        }
        return false;
    }

    private void InputText(AccessibilityNodeInfo node, String text) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );
        // 先聚焦，再输入
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);
    }

    @Override
    public void onInterrupt() {}
}