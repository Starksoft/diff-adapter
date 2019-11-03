//package ru.starksoft.differ.sample.base.viewholder;
//
//import android.view.ViewGroup;
//
//import androidx.annotation.LayoutRes;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.databinding.DataBindingUtil;
//import androidx.databinding.ViewDataBinding;
//
//import ru.starksoft.differ.adapter.OnClickListener;
//import ru.starksoft.differ.viewmodel.ViewModel;
//
//public abstract class BaseBinderViewHolder <M extends ViewModel> extends BaseViewHolder<M> {
//
//	@Nullable private final ViewDataBinding binding;
//
//	public BaseBinderViewHolder(@LayoutRes int layout, @NonNull ViewGroup parent, @Nullable OnClickListener onClickListener) {
//		super(layout, parent, onClickListener);
//		binding = DataBindingUtil.bind(itemView);
//	}
//
//	@NonNull
//	public Optional<ViewDataBinding> getBinding() {
//		return Optional.ofNullable(binding);
//	}
//
//	public abstract int getBindingVariableViewModel();
//
//	@Override
//	protected void bind(@NonNull M viewModel) {
//		getBinding().ifPresent(binding -> {
//			binding.setVariable(getBindingVariableViewModel(), viewModel);
//			binding.executePendingBindings();
//		});
//	}
//
//}
