package com.example.awesome.thanxdude;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        mAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);


        if(mAuth.getCurrentUser() != null)
        {
            mFirebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = mFirebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);


            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges())
                    {
                        if( doc.getType() == DocumentChange.Type.ADDED)
                        {
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;

    }

}
