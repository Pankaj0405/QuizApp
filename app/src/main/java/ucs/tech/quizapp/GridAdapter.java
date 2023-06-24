package ucs.tech.quizapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    private List<String > sets ;
    private String Category;

    public GridAdapter(List<String> sets,String Category)
    {
        this.sets = sets;
        this.Category=Category;
    }

    @Override
    public int getCount() {
        return sets.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view;

        if (convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item,parent,false);
        }else{
            view = convertView;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questionintent = new Intent(parent.getContext(),Questions_Activity.class);
                questionintent.putExtra("category",Category);
                questionintent.putExtra("setId",sets.get(position));

                parent.getContext().startActivity(questionintent);
            }
        });

        ((TextView)view.findViewById(R.id.setTextview)).setText(String.valueOf(position+1));

        return view;


    }
}
