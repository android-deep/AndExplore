package com.beingyi.app.AE.activity;

import com.beingyi.app.AE.utils.AesUtil;
import com.beingyi.app.AE.utils.BASE128;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.beingyi.app.AE.R;
import com.beingyi.app.AE.base.BaseActivity;
import com.beingyi.app.AE.dialog.alert;
import com.beingyi.app.AE.ui.AlertProgress;
import com.beingyi.app.AE.utils.ClassTree;
import com.beingyi.app.AE.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.beingyi.app.AE.ui.ToastUtils;
import com.beingyi.app.AE.utils.editor.ARSCEditor;
import com.beingyi.app.AE.utils.editor.AXmlEditor;
import com.beingyi.app.AE.utils.editor.Edit;
import com.beingyi.app.AE.view.CodeEditor;
import com.bigzhao.xml2axml.AxmlUtils;
import com.myopicmobile.textwarrior.common.Language;
import com.myopicmobile.textwarrior.common.LanguageJava;
import com.myopicmobile.textwarrior.common.LanguageSmali;

import org.apache.commons.lang3.StringUtils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;

public class TextEditor extends BaseActivity implements View.OnClickListener{


    public static byte[] output;
    public byte[] input;
    public String Path;
    public boolean isFile;

    public static ClassTree classTree;
    public boolean isSmali = false;


    Edit edit;

    Context context;
    Activity activity;
    CodeEditor editor;
    String content = null;

    Toolbar toolbar;

    LinearLayout ln_bottom;
    AppCompatEditText ed_find_content;
    Button btn_find_up;
    Button btn_find_down;



    boolean isChanged = false;
    boolean isInZip = false;
    boolean isViewText = true;
    boolean isEditAxml = false;


    public void init() {
        context = this;
        activity = this;
        toolbar = (Toolbar) findViewById(R.id.activity_texteditor_Toolbar);
        editor = findViewById(R.id.activity_texteditor_CodeEditor);

        ln_bottom=find(R.id.activity_texteditor_LinearLayout_bottom);
        ed_find_content=find(R.id.activity_texteditor_AppCompatEditText_find_content);
        btn_find_up=find(R.id.activity_texteditor_Button_up);
        btn_find_down=find(R.id.activity_texteditor_Button_down);

        btn_find_up.setOnClickListener(this);
        btn_find_down.setOnClickListener(this);

    }

    /**
     * 自动调节输入框高度
     */
    public void openWrapInputMode() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openWrapInputMode();
        setContentView(R.layout.activity_texteditor);
        init();



        Intent intent = getIntent();
        isFile = intent.getBooleanExtra(BASE128.decode("/h+2Zkr8crYDBzmTRWy5Cg=="), true);
        isInZip = intent.getBooleanExtra(BASE128.decode("M13MUqoK/AAi9GI5H/keSA=="), false);
        Path = intent.getStringExtra(BASE128.decode("cr62CT1jqczIn9Pg6z6GZg=="));

        input = intent.getByteArrayExtra(BASE128.decode("5LauhhqS4nOX+qM2+Dz8FQ=="));
        isSmali = intent.getBooleanExtra(BASE128.decode("9lEtzJiab3u60p/FgJuLvw=="), false);
        isEditAxml = intent.getBooleanExtra(BASE128.decode("aDp+/gaMqx2Pa8OZlGXlQA=="), false);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        ln_bottom.setVisibility(View.GONE);

        if (Path != null) {

            getSupportActionBar().setTitle(new File(Path).getName());

            if (FileUtils.getSuffix(Path).equals(BASE128.decode("HkklReTLpauVSMXzvfIEZw=="))) {
                //editor.setType(CodeEditor.JAVA);
            }

        }

        final AlertProgress progres = new AlertProgress(context);
        new Thread() {
            @Override
            public void run() {


                progres.setLabel(BASE128.decode("NG6/2bPeC5kiLGc6CtSQOA=="));
                progres.setNoProgress();
                progres.show();

                try {

                    if (isFile) {

                        if (FileUtils.isBinary(Path)) {
                            setBinContent();
                        } else {
                            setContent();
                        }

                    }

                    if (input != null) {
                        content = new String(input, BASE128.decode("KYnYPK9d1g6ki50IhIT7JQ=="));
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                setWatcher();
                            }
                        });

                    }


                } catch (Exception e) {
                    new alert(context, e.toString());
                }

                progres.dismiss();

            }
        }.start();


        if (!isViewText) {

        }


    }


    public void setBinContent() throws Exception {

        String suffix = FileUtils.getSuffix(Path);
        if (suffix.equals(BASE128.decode("DOVaWgpEFAam1oCBFmeVKQ=="))) {
            isViewText = false;
            edit = new ARSCEditor();
        }

        if (suffix.equals(BASE128.decode("0FbaDnz4QEd8OKxUL8aYkA=="))) {
            isViewText = false;
            if (isEditAxml) {
                content = AxmlUtils.decode(new File(Path));
                setWatcher();
                return;
            } else {
                edit = new AXmlEditor();
            }
        }

        List<String> list = new ArrayList<String>();
        if (edit != null) {
            edit.read(list, FileUtils.getByte(Path));
            content = StringUtils.join(list, "\n");
            setWatcher();

        } else {
            //new alert(context, "不支持此类文件的编辑");
            setContent();
        }


    }


    public void setContent() {


        String suffix = FileUtils.getSuffix(Path);

        if (isViewText) {
            content = FileUtils.readFile(Path);
            setWatcher();
        }

    }


    public void setWatcher() {

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                editor.setText(content);
                editor.gotoLine(0);

                editor.focusCaret();
                if (isSmali) {
                    Language lang = new LanguageSmali();
                    editor.setLanguage(lang);
                }

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_texteditor_undo:
                editor.undo();
                break;

            case R.id.action_texteditor_redo:
                editor.redo();
                break;

            case R.id.action_texteditor_find:
                ln_bottom.setVisibility(View.VISIBLE);
                break;

            case R.id.action_texteditor_save:
                save(false);
                break;

            case R.id.action_texteditor_exit:
                exit();
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void exit(){

        isChanged=editor.isEdited();
        if (isChanged) {

            tip();

        } else {
            finish();
            input = null;
        }


    }

    public void save(boolean isExit) {

        final AlertProgress progres = new AlertProgress(context);
        new Thread() {
            @Override
            public void run() {
                progres.setLabel(BASE128.decode("NG6/2bPeC5kiLGc6CtSQOA=="));
                progres.setNoProgress();
                progres.show();


                try {

                    String data = editor.getText().toString();

                    if (Path != null) {


                        FileUtils.createFile(Path);
                        if (isViewText) {

                            if (input == null) {

                                final FileOutputStream outStream = new FileOutputStream(Path);
                                outStream.write(editor.getText().toString().getBytes());
                                outStream.close();

                            } else {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                edit.write(data, baos);
                                TextEditor.output = baos.toByteArray();
                                input = null;
                            }
                            editor.setEdited(false);

                        } else {

                            if (!isEditAxml) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                edit.write(data, baos);
                                TextEditor.output = baos.toByteArray();
                                input = null;
                                if (isFile) {
                                    final FileOutputStream outStream = new FileOutputStream(Path);
                                    outStream.write(baos.toByteArray());
                                    outStream.close();
                                }
                            } else {
                                byte[] axml = AxmlUtils.encodeString(content);
                                FileUtils.saveFile(axml, Path);
                            }
                            editor.setEdited(false);

                        }

                    }


                    if (Path == null) {

                        if (isSmali) {
                            DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());
                            dexBuilder.setIgnoreMethodAndFieldError(true);
                            ClassDef classDef = Smali.assembleSmaliFile(data, dexBuilder, new SmaliOptions());
                            classTree.saveClassDef(classDef);
                            DexEditor.isChanged = true;
                            editor.setEdited(false);
                        }


                    }


                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.show(BASE128.decode("/LjcHni4sYVzlI1JuttI3w=="));
                            isChanged = false;
                            if (isExit) {
                                input = null;
                                Path = null;
                                finish();
                            }
                        }
                    });


                } catch (Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new alert(context, BASE128.decode("5x3zP/7tNw0g1zTyNGUeyQ==") + e.toString());
                        }
                    });
                }

                progres.dismiss();
            }
        }.start();


    }


    public void tip() {


        AlertDialog dialog = new AlertDialog.Builder(context)

                .setMessage(BASE128.decode("aoVoUaQMGlZ1r0Vbg0bzoQg3wbGi7GQ5CFa6kO3TnxM="))
                .setCancelable(true)
                .setNegativeButton(BASE128.decode("nl3LtLQgi6JmM2vbI7spGA=="), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        input = null;
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(BASE128.decode("gba256PHP6LGkSXBwqlv8Q=="), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(BASE128.decode("OAdbWqNNw0PI+XiWtlzAvw=="), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        save(true);
                    }
                })
                .create();
        dialog.show();


    }

    @Override
    public void onClick(View view) {
        int id=view.getId();
        switch (id){

            case R.id.activity_texteditor_Button_up:
                editor.findUp(ed_find_content.getText().toString());
                break;

            case R.id.activity_texteditor_Button_down:
                editor.findDown(ed_find_content.getText().toString());
                break;


        }
    }


    class Text implements Edit {

        public void read(List<String> data, byte[] input) throws IOException {
            String s = new String(input, BASE128.decode("KYnYPK9d1g6ki50IhIT7JQ=="));
            String[] strs = s.split("\n");
            for (String str : strs) {
                data.add(str);
            }
        }

        public void write(String data, OutputStream out) throws IOException {
            out.write(data.getBytes(BASE128.decode("KYnYPK9d1g6ki50IhIT7JQ==")));
        }
    }


}
