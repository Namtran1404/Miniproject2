package com.example.miniproject2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        adapter = new TitleAdapter(this, titles,guids);
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
    public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.TitleViewHolder> {
        private List<String> titles;
        private Context context;
        private List<String> guids;

        public TitleAdapter(Context context, List<String> titles, List<String> guids) {
            this.context = context;
            this.titles = titles;
            this.guids = guids; // Gán danh sách guids
        }

        @NonNull
        @Override
        public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
            return new TitleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
            String title = titles.get(position);
            int maxLength = 40; // Đặt chiều dài tối đa bạn muốn
            if (title.length() > maxLength) {
                title = title.substring(0, maxLength) + "...";
            }
            holder.textView.setText(title);
            // Sử dụng guids.get(position) để lấy guid tương ứng
            String guid = guids.get(position);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Xử lý sự kiện khi nút Button được nhấn
                    openBrowser(guid); // Gọi phương thức mở trình duyệt với guid tương ứng
                }
            });
        }
        private void openBrowser(String guid) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(guid));
            startActivity(i);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }


        public class TitleViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            Button button;

            public TitleViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
                button = itemView.findViewById(R.id.button);
            }
        }
    }
    private class DownloadTask extends AsyncTask<String, Void, List<Pair<String, String>>> {

        @Override
        protected List<Pair<String, String>> doInBackground(String... urls) {
            List<Pair<String, String>> titleGuidPairs = new ArrayList<>();
            try {
                // Kết nối tới URL và lấy HTML
                Document doc = Jsoup.connect(urls[0]).get();
                // Trích xuất tiêu đề và guid từ các mục trong tài liệu RSS
                Elements items = doc.select("item");
                for (Element item : items) {
                    String title = item.select("title").first().text();
                    String guid = item.select("guid").first().text();
                    // Thêm cặp tiêu đề và guid vào danh sách
                    titleGuidPairs.add(new Pair<>(title, guid));
                }
            } catch (IOException e) {
                Log.e("DownloadTask", "Error downloading RSS", e);
            }
            return titleGuidPairs;
        }
        @Override
        protected void onPostExecute(List<Pair<String, String>> titleGuidPairs) {
            super.onPostExecute(titleGuidPairs);
            // Sau khi tải xong, bạn có thể cập nhật RecyclerView hoặc thực hiện các thao tác khác ở đây
            for (Pair<String, String> pair : titleGuidPairs) {
                String title = pair.first;
                String guid = pair.second;
                MainActivity.this.titles.add(title);
                MainActivity.this.guids.add(guid);
            }
            adapter.notifyDataSetChanged();
        }
    }

}