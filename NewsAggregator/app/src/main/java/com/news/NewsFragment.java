package com.news;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.ParseException;

public class NewsFragment extends Fragment {
    private String url;

    public NewsFragment() { }

    public static NewsFragment newInstance(NewsArticle article, int index, int max) {
        NewsFragment f = new NewsFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("ARTICLE", article);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL", max);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragment_layout = inflater.inflate(R.layout.news_fragment, container, false);

        Bundle args = getArguments();
        if (args != null) {
            final NewsArticle currentArticle = (NewsArticle) args.getSerializable("ARTICLE");
            if (currentArticle == null) {
                return null;
            }

            int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL");

            TextView headline = fragment_layout.findViewById(R.id.fragmentHeadline);
            TextView date = fragment_layout.findViewById(R.id.fragmentDate);
            TextView author = fragment_layout.findViewById(R.id.fragmentAuthor);
            ImageView image = fragment_layout.findViewById(R.id.fragmentImage);
            TextView content = fragment_layout.findViewById(R.id.fragmentContent);
            TextView counter = fragment_layout.findViewById(R.id.pageNum);

            headline.setText(currentArticle.getHeadline());

            try { date.setText(currentArticle.getDate()); }
            catch (ParseException e) { e.printStackTrace(); }

            if (!currentArticle.getAuthor().equals("null")) author.setText(currentArticle.getAuthor());
            else author.setText(R.string.no_author);
            content.setText(currentArticle.getContent());
            counter.setText(String.format("%d of %d", index, total));

            Picasso.get()
                .load(currentArticle.getImageURL())
                .error(R.drawable.noimage)
                .placeholder(R.drawable.brokenimage)
                .into(image);

            if (!currentArticle.getArticleURL().equals("")) {
                url = currentArticle.getArticleURL();

                headline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!headline.toString().equals("")) openArticle(v);
                    }
                });

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openArticle(v);
                    }
                });

                content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!content.toString().equals("")) openArticle(v);
                    }
                });
            }

            return fragment_layout;
        }
        else return null;
    }

    public void openArticle(View v) {
        Intent articleIntent = new Intent(Intent.ACTION_VIEW);
        articleIntent.setData(Uri.parse(url));
        startActivity(articleIntent);
    }
}
