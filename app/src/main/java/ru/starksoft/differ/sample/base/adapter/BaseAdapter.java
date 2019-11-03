package ru.starksoft.differ.sample.base.adapter;

import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import ru.starksoft.differ.Logger;
import ru.starksoft.differ.adapter.DifferAdapter;
import ru.starksoft.differ.adapter.OnClickListener;
import ru.starksoft.differ.sample.base.divider.SeparatorType;
import ru.starksoft.differ.sample.base.viewmodel.BaseViewModel;
import ru.starksoft.differ.viewholder.DifferViewHolder;
import ru.starksoft.differ.viewmodel.ViewModel;

public abstract class BaseAdapter extends DifferAdapter {

	private static final Logger LOGGER = new Logger() {
		@Override
		public void log(@NonNull Throwable t) {

		}

		@Override
		public void log(@NonNull String tag, @NonNull String message) {

		}

		@Override
		public void log(@NonNull String tag, @NonNull String message, @NonNull Throwable t) {

		}

		@Override
		public void w(@NonNull String tag, @NonNull String message) {

		}

		@Override
		public void d(@NonNull String tag, @NonNull String message) {

		}
	};

	public BaseAdapter(@NonNull OnClickListener onClickListener) {
		super(onClickListener, LOGGER);
	}

	public BaseAdapter(@NonNull OnClickListener onClickListener, @NonNull List<ViewModel> list) {
		super(onClickListener, list, LOGGER);
	}

	public BaseAdapter(@NonNull OnClickListener onClickListener, @NonNull ViewModel[] array) {
		super(onClickListener, array, LOGGER);
	}

	@NonNull
	public static synchronized String getElementInfo(@Nullable List<? extends ViewModel> list, int position) {
		StringBuilder stringBuilder = new StringBuilder();
		final boolean empty = list != null && list.isEmpty();
		stringBuilder.append("\n listSize = ").append("(").append(empty ? "null" : list.size()).append(")");

		if (!empty) {
			final ViewModel item = list.size() > position ? list.get(position) : null;
			String itemContent = item != null ? item.toString() : "null";
			stringBuilder.append(" item").append("(").append(position).append(")").append(" = ").append(itemContent);
		}
		return stringBuilder.toString();
	}

	@NonNull
	@Override
	public final DifferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return super.onCreateViewHolder(parent, viewType);
	}

	@Override
	public void setMaxRecycledViews(@NonNull RecyclerView recyclerView) {
	}

	@SeparatorType
	public int getDividerType(@IntRange(from = 0) int position) {
		Class classViewModel = getClassViewModelForLastSpecialDivider();
		if (classViewModel != null) {
			boolean isLastItem = position == getItemCount() - 2;
			if (isLastItem) {
				//noinspection unchecked
				boolean isLastSpecialDivider = getLastItem().getItemViewType() == BaseViewModel.getItemViewType(classViewModel);
				if (isLastSpecialDivider) {
					return SeparatorType.SEPARATOR_PADDING_16;
				}
			}
		}

		ViewModel viewModel = getItem(position);
		return viewModel != null ? viewModel.getDividerType() : SeparatorType.SEPARATOR_WITHOUT_PADDING;
	}

	/**
	 * Если перед последних элементов списка нужен особый тип Divider,
	 * то нужно переопределить этот метод и вернуть класс ViewModel,
	 * который сигнализирует о классе ViewModel последнего элемента,
	 * перед которым нужен это divider.
	 * <p>
	 * Например, последним элементом списка вакансий идет лоадер дозагрузки страницы с типом LoadingViewModel.
	 * Перед эти лоадером divider должен иметь отступы с двух сторон
	 *
	 * @return Class ViewModel
	 */
	@Nullable
	protected Class<? extends ViewModel> getClassViewModelForLastSpecialDivider() {
		return null;
	}
}
