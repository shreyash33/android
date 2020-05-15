package com.example.awesome.thanxdude;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blog_list1;
    public Context mContext;

    private FirebaseFirestore mFirebaseFirestore;



    public BlogRecyclerAdapter(List<BlogPost> blog_list){
            this.blog_list1 = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent,false);

        mContext = parent.getContext();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String desc_data = blog_list1.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blog_list1.get(position).getImage_url();
        holder.setBlogImage(image_url);

        String user_id = blog_list1.get(position).getUser_id();
        //User data will be retrived here...
        mFirebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    String userName = task.getResult().getString("UserName");
                    String phone = task.getResult().getString("MobileNumber");
                    String userImage = task.getResult().getString("ProfileImage");

                    holder.setUserData(userName,phone,userImage);

                }
                else
                {
                        //Firebase Exceptions
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return blog_list1.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private EditText descView;
        private ImageView blogImageView;

        private EditText blogUserName;
        private EditText blogUserNumber;
        private CircleImageView blogUserImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;


        }
        public void setDescText(String desctext){
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(desctext);
        }

        public void setBlogImage(String DownloadUri){
            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.add_post);
            Glide.with(mContext).load(DownloadUri).into(blogImageView);
        }


        public void setUserData(String userName, String phone , String userImage){
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserNumber = mView.findViewById(R.id.blog_user_number);

            blogUserName.setText(userName);
            blogUserNumber.setText(phone);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.default_image);

            Glide.with(mContext).applyDefaultRequestOptions(placeholderOption).load(userImage).into(blogUserImage);

        }

    }
}
