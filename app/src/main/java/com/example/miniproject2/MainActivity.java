package com.example.miniproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private Button loadButton;
    private RecyclerView recyclerView;
    private List<String> titles;
    private List<String> guids;
    private TitleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Khởi tạo
        editText = findViewById(R.id.editText);
        loadButton = findViewById(R.id.loadButton);
        recyclerView = findViewById(R.id.recyclerView);
        titles = new ArrayList<>();
        guids = new ArrayList<>();
        adapter = new TitleAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Thiết lập sự kiện click cho nút Load
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy URL từ EditText
                String url = editText.getText().toString();
                // Nếu URL không rỗng, thực hiện tải RSS
                if (!url.isEmpty()) {
                    new DownloadTask().execute(url);
                }
            }
        });
    }
    private class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.TitleViewHolder> {
        @Override
        public void onBindViewHolder(@NonNull TitleViewHolder holder, int position, @NonNull List<Object> payloads) {
            String title = titles.get(position);
            String guid = guids.get(position); // Giả sử bạn có một danh sách guids

            // Giới hạn độ dài của tiêu đề
            int maxLength = 20; // Đặt chiều dài tối đa bạn muốn
            if (title.length() > maxLength) {
                title = title.substring(0, maxLength) + "...";
            }

            holder.textView.setText(title);
            holder.openButton.setTag(guid); // Gán giá trị của guid vào tag của nút Button
        }

        @NonNull
        @Override
        public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TitleViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
            String title = titles.get(position);
            holder.textView.setText(title);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }


        class TitleViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            Button openButton;

            public TitleViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
                openButton = itemView.findViewById(android.R.id.button1);

                // Thiết lập sự kiện click cho nút Button
                openButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String guid = (String) v.getTag(); // Lấy giá trị của guid từ tag của nút Button
                        // Mở trình duyệt với URL tương ứng
                        if (guid != null && !guid.isEmpty()) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(guid));
                            startActivity(browserIntent);
                        }
                    }
                });
            }
        }
    }
    private class DownloadTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> titles = new ArrayList<>();
            try {
                // Kết nối tới URL và lấy HTML
                Document doc = Jsoup.connect(urls[0]).get();
                // Trích xuất tiêu đề từ các thẻ <title>
                Elements elements = doc.select("title");
                for (Element element : elements) {
                    // Thêm tiêu đề vào danh sách
                    titles.add(element.text());
                }
            } catch (IOException e) {
                Log.e("DownloadTask", "Error downloading RSS", e);
            }
            return titles;
        }

        @Override
        protected void onPostExecute(List<String> titles) {
            super.onPostExecute(titles);
            // Cập nhật RecyclerView sau khi tải xong
            MainActivity.this.titles.addAll(titles);
            adapter.notifyDataSetChanged();
        }
    }
}