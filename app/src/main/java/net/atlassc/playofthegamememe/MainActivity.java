package net.atlassc.playofthegamememe;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import net.atlassc.playofthegamememe.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQ_CAPTURE_PHOTO = 1118;
    private ActivityMainBinding uiBinding;
    private Uri fileUri;

    private static final String _TEMP_PHOTO_PATH = "temp_photo_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PermissionsUtil.checkPermissions(this, PERMISSION);
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setTypeFace();
        setInputActions();

        uiBinding.preview.setDrawingCacheEnabled(true);

        setButtonActions();

        setupDragingText();
//
//        Glide.with(this)
//                .load(R.drawable.dva)
//                .into(uiBinding.image);

    }

    private void setupDragingText() {
        uiBinding.textGroup
                .setOnTouchListener(new View.OnTouchListener() {
                    private float lastY;
                    private float lastX;

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            lastX = motionEvent.getX();
                            lastY = motionEvent.getY();
                        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                            float x = motionEvent.getX();
                            float y = motionEvent.getY();
                            float distX = x - lastX;
                            float distY = y - lastY;

                            view.setX(view.getX() + distX);
                            view.setY(view.getY() + distY);
                            lastX = motionEvent.getX();
                            lastY = motionEvent.getY();

                        } else {

                        }
                        return false;
                    }
                });
    }

    private void setButtonActions() {
        uiBinding.selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crop.pickImage(MainActivity.this);
            }
        });
        uiBinding.capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = MediaUtil.getOutputMediaFileUri(MediaUtil.MEDIA_TYPE_IMAGE); // create a file to save the image
                if (fileUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                    // cache uri
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                            .edit().putString(_TEMP_PHOTO_PATH, fileUri.getPath()).apply();
                    // start the image capture Intent
                    startActivityForResult(intent, REQ_CAPTURE_PHOTO);
                } else {
                    Snackbar.make(uiBinding.rootView, "sorry, start cam failed.", Snackbar.LENGTH_LONG).show();
                }

            }
        });

        uiBinding.saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    Bitmap bitmap = uiBinding.preview.getDrawingCache();
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                            "/PlayOfTheGame/POTG_" + System.currentTimeMillis() + ".jpg";
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream stream = new FileOutputStream(
                            file.toString());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    ContentValues values = new ContentValues();

                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, filePath);

                    MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Snackbar.make(uiBinding.rootView, "image saved to gallery @ " + filePath, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(uiBinding.rootView, "image saving failed, please check storage permissions.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        uiBinding.shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    Bitmap bitmap = uiBinding.preview.getDrawingCache();
                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                            "/PlayOfTheGame/POTG_" + System.currentTimeMillis() + ".jpg";
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }

                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "PLAY OF THE GAME", "PLAY OF THE GAME MEME");
                    Uri uri = Uri.parse(path);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share Image"));

                    Snackbar.make(uiBinding.rootView, "image saved to gallery @ " + filePath, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(uiBinding.rootView, "image cache creating failed, please check storage permissions.", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
            fileUri = null;
        } else if (requestCode == REQ_CAPTURE_PHOTO) {

            beginCrop(fileUri);
        }
    }

    private void beginCrop(Uri source) {
        if (source == null) {
            Snackbar.make(uiBinding.rootView, "image not selected", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Uri destination = null;
        try {
            destination = Uri.fromFile(new File(getCacheDir(), "cropped_" + UUID.randomUUID()));
        } catch (Exception e) {
            Snackbar.make(uiBinding.rootView, "image crop failed", Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (destination != null) {
            Crop.of(source, destination).withAspect(16, 9).start(this);
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Glide.with(this)
                    .load(Crop.getOutput(result))
                    .into(uiBinding.image);

            uiBinding.preview.destroyDrawingCache();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setInputActions() {
        uiBinding.inputPotg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence == null || charSequence.length() == 0 || charSequence.equals("")) {
                    charSequence = "PLAY OF THE GAME   ";
                }
                String text = charSequence.toString().toUpperCase() + "  ";
                Spanned spanned = Html.fromHtml(text);
                uiBinding.textPotg.setText(spanned);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        uiBinding.inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence == null || charSequence.length() == 0 || charSequence.equals("")) {
                    charSequence = "nobody";
                }
                String text = charSequence.toString().toUpperCase() + "  ";
                Spanned spanned = Html.fromHtml(text);
                uiBinding.textName.setText(spanned);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        uiBinding.inputCharacter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence == null || charSequence.length() == 0 || charSequence.equals("")) {
                    charSequence = "no one";
                }
                uiBinding.textCharacter.setText("AS " + charSequence.toString().toUpperCase() + "  ");

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setTypeFace() {
        Typeface face = null;
        try {
            face = Typeface.createFromAsset(getAssets(),
                    "fonts/bignoodletoo.woff");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (face == null) {
            face = Typeface.createFromAsset(getAssets(),
                    "bignoodletoo.ttf");
        }

        if (face != null) {
            uiBinding.titleLine.setTypeface(face);
            uiBinding.textPotg.setTypeface(face);
            uiBinding.textName.setTypeface(face);
            uiBinding.textCharacter.setTypeface(face);
            uiBinding.inputPotg.setTypeface(face);
            uiBinding.inputName.setTypeface(face);
            uiBinding.inputCharacter.setTypeface(face);
            uiBinding.inputPotgLayout.setTypeface(face);
            uiBinding.inputNameLayout.setTypeface(face);
            uiBinding.inputCharacterLayout.setTypeface(face);
            uiBinding.selectImage.setTypeface(face);
            uiBinding.capturePhoto.setTypeface(face);
            uiBinding.saveImage.setTypeface(face);
            uiBinding.shareImage.setTypeface(face);
            uiBinding.tips.setTypeface(face);
        } else {
            uiBinding.titleLine.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            uiBinding.titleLine.setText("Sorry, font not found.");
        }


    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adjustPreviewRatio();
    }

    private void adjustPreviewRatio() {
        uiBinding.preview.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ViewGroup.LayoutParams lp = uiBinding.preview.getLayoutParams();
                    int measuredWidth = uiBinding.preview.getMeasuredWidth();
                    lp.height = measuredWidth / 16 * 9;
                    uiBinding.preview.setLayoutParams(lp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (uiBinding.preview.getMeasuredWidth() == 0) {
            adjustPreviewRatio();
        }


    }
}
