# diff-adapter [![Build Status](https://app.bitrise.io/app/6553aa44cdd9b3ff/status.svg?token=rYMW7z9wx6R-g8OBBVjYFA&branch=master)](https://app.bitrise.io/app/6553aa44cdd9b3ff) [![Maven Central](https://img.shields.io/maven-central/v/ru.starksoft/diff-adapter)](https://repo1.maven.org/maven2/ru/starksoft/diff-adapter/)

Gradle:

```
implementation 'ru.starksoft:diff-adapter:1.x.x'
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
    .initAdapter()
    .attachTo(sampleRecyclerView, createDifferAdapterEventListener(), refreshAdapterOnAttach = true)
```

If you are using reflection-based factory **.withViewHolders** add this in proguard file: 

```
-keep public class * extends ru.starksoft.differ.adapter.viewholder.DifferViewHolder {
    *;
}
```  

# License:
```
            GNU LESSER GENERAL PUBLIC LICENSE
		       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]

```
