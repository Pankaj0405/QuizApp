package ucs.tech.quizapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class catogoriesAdapter extends RecyclerView.Adapter<catogoriesAdapter.viewholder> {
    private List<catogoriesModel> catogoriesModelList;

    public catogoriesAdapter(List<catogoriesModel> catogoriesModelList) {
        this.catogoriesModelList = catogoriesModelList;
    }


    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catogories_item,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        holder.setData(catogoriesModelList.get(position).getUrl(),catogoriesModelList.get(position).getName(),position);
    }

    @Override
    public int getItemCount() {
        return catogoriesModelList.size();
    }

    class viewholder extends RecyclerView.ViewHolder{

        private CircleImageView imageview;
        private TextView title;

        public viewholder(@NonNull View itemView) {
            super(itemView);

            imageview = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.Title);
        }

        private void setData(String url, final String title, final int position){
            Glide.with(itemView.getContext()).load(url).into(imageview);
            this.title.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(itemView.getContext(), "Hello", Toast.LENGTH_SHORT).show();
                    Intent setIntent = new Intent(itemView.getContext(),SetsActivity.class);
                    setIntent.putExtra("Title",title);
                    setIntent.putExtra("position",position);
                    itemView.getContext().startActivity(setIntent);
                }
            });
        }
    }
}
