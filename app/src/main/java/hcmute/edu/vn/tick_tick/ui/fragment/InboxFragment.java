package hcmute.edu.vn.tick_tick.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.tick_tick.R;
import hcmute.edu.vn.tick_tick.ui.adapter.TaskAdapter;
import hcmute.edu.vn.tick_tick.viewmodel.TaskViewModel;

public class InboxFragment extends Fragment {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private View viewEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_inbox);
        viewEmpty = view.findViewById(R.id.view_empty);

        adapter = new TaskAdapter(viewModel, getParentFragmentManager());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getInboxTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);
            viewEmpty.setVisibility(tasks == null || tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
