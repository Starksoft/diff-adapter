# diff-adapter [![Build Status](https://app.bitrise.io/app/6553aa44cdd9b3ff/status.svg?token=rYMW7z9wx6R-g8OBBVjYFA&branch=master)](https://app.bitrise.io/app/6553aa44cdd9b3ff) [![Download](https://api.bintray.com/packages/edwardstark/android-maven/diff-adapter/images/download.svg) ](https://bintray.com/edwardstark/android-maven/diff-adapter/_latestVersion)



```
implementation 'ru.starksoft:diff-adapter:1.0.X'
```

Usage:

```
DiffAdapter
			.create(adapterDataSource)
      // Reflection-based factory
			.withViewHolders(SampleViewHolder::class.java, HeaderViewHolder::class.java, DataInfoViewHolder::class.java)
			//	Second variant to attach ViewHolders, without reflection
			.withFactory(ViewHolderFactory { parent, viewType, onClickListener ->
				return@ViewHolderFactory when (viewType) {
					DifferViewModel.getItemViewType(SampleViewModel::class.java) -> SampleViewHolder(parent, onClickListener)
					DifferViewModel.getItemViewType(HeaderViewModel::class.java) -> HeaderViewHolder(parent, onClickListener)
					DifferViewModel.getItemViewType(DataInfoViewModel::class.java) -> DataInfoViewHolder(parent, onClickListener)

					else -> throw IllegalStateException("Unknown viewType=$viewType at ${javaClass.simpleName}")
				}
			})
			.withClickListener(OnClickListener { _, viewModel, action, _ ->
				return@OnClickListener when (action) {
					SampleClickAction.DELETE.ordinal -> {
						adapterDataSource.remove((viewModel as SampleViewModel).id)
						true
					}
					SampleClickAction.DELETE_MULTI.ordinal -> {
						(activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
						true
					}
					else -> false
				}
			})
			.attachTo(sampleRecyclerView, createDifferAdapterEventListener(), refreshAdapterOnAttach = true)

```
