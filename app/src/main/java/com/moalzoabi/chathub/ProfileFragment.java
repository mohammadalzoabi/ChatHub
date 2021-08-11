package com.moalzoabi.chathub;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    TextView username;
    ImageView profileImage;

    Button changeImage;
    Button changeUsername;
    Button changeStatus;
    Button accountSettings;
    Button logOut;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    StorageReference storageReference;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileTabImage);
        username = view.findViewById(R.id.profileTabUsername);

        changeImage = view.findViewById(R.id.changePictureButton);
        changeUsername = view.findViewById(R.id.changeUsernameButton);
        changeStatus = view.findViewById(R.id.changeBioButton);
        accountSettings = view.findViewById(R.id.accountSettingsButton);
        logOut = view.findViewById(R.id.logOutButton);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                Users user = snapshot.getValue(Users.class);
                username.setText(user.getUsername());

                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.drawable.logo);
                } else {
                    if (getActivity() != null) {
                        Glide.with(getContext())
                                .load(user.getImageURL())
                                .dontAnimate()
                                .fitCenter()
                                .placeholder(profileImage.getDrawable())
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNameDialog();
            }
        });
        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeStatusDialog();
            }
        });
        accountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountSettingsDialog();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Are you sure you want to log out?");
                builder.setIcon(R.drawable.ic_logout);
                builder.setPositiveButton("Log out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage("Logging Out");
                        progressDialog.show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        return view;
    }

    private void SelectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void UploadMyImage() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));


            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        String mUri = uri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        progressDialog.dismiss();

                        Toast.makeText(getContext(), "Profile Picture Changed Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        } else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();
            } else {
                UploadMyImage();
            }
        }
    }

    private void showChangeNameDialog(){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.name_update_dialog, null);

        final EditText newName = view.findViewById(R.id.newNameEditText);
        Button changeName = view.findViewById(R.id.changeNameButton);
        TextView countdownTextView = view.findViewById(R.id.countdownTextView);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                newName.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                countdownTextView.setText(20-s.toString().length() + "/20");
            }
        });

        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = newName.getText().toString();

                if(TextUtils.isEmpty(name)){
                    Toast.makeText(getActivity(), "Name Cannot Be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
                HashMap<String, Object> map = new HashMap<>();
                map.put("username", name);
                reference.updateChildren(map);
                Toast.makeText(getActivity(), "Name Changed Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showChangeStatusDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.status_update_dialog, null);

        final EditText newStatus = view.findViewById(R.id.newBioEditText);
        Button changeStatus = view.findViewById(R.id.changeBioButton);
        TextView countdownTextView = view.findViewById(R.id.countdownTextView);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                newStatus.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        newStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                countdownTextView.setText(25-s.toString().length() + "/25");
            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = newStatus.getText().toString();

                dialog.dismiss();
                reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
                HashMap<String, Object> map = new HashMap<>();
                map.put("bio", status);
                reference.updateChildren(map);
                Toast.makeText(getActivity(), "Status Changed Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAccountSettingsDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.account_settings_dialog, null);

        Button changeEmail = view.findViewById(R.id.changeEmailButton);;
        Button changePassword = view.findViewById(R.id.changePasswordButton);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeEmailDialog();
                dialog.dismiss();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
                dialog.dismiss();
            }
        });
    }

    private void showChangePasswordDialog(){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.password_update_dialog, null);

        final EditText currentPassword = view.findViewById(R.id.oldPasswordEditText);
        final EditText newPassword2 = view.findViewById(R.id.newPasswordEditText);
        Button changePassword = view.findViewById(R.id.changePasswordButton2);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = currentPassword.getText().toString();
                String newPassword = newPassword2.getText().toString();

                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Please Enter Your Current Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<8){
                    Toast.makeText(getActivity(), "The New Password Must Be At Least 8 Characters Long", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.show();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseUser.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getActivity(), "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }

    private void showChangeEmailDialog(){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.email_update_dialog, null);

        final EditText newEmail = view.findViewById(R.id.newEmailEditText);
        final EditText currentPassword = view.findViewById(R.id.PasswordEditText);
        Button changeEmail = view.findViewById(R.id.changeEmailButton2);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmailAddress = newEmail.getText().toString();
                String password = currentPassword.getText().toString();

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getActivity(), "Please Enter Your Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(newEmailAddress).matches()){
                    Toast.makeText(getActivity(), "Please Enter A Valid Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updateEmail(newEmailAddress, password);
            }
        });
    }

    private void updateEmail(String newEmail, String password){
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading");
        progressDialog.show();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), password);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseUser.updateEmail(newEmail)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getActivity(), "Email Changed Successfully, a Verification Email Has Been Sent", Toast.LENGTH_SHORT).show();
                                        firebaseUser.sendEmailVerification();
                                        progressDialog.dismiss();
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage() , Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }
}