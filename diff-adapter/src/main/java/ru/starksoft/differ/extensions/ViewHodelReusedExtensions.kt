package ru.starksoft.differ.extensions

import ru.starksoft.differ.viewmodel.ViewModel
import ru.starksoft.differ.viewmodel.ViewModelReused

/**
 * @objectList - Список объектов по которым будет высчитываться хэш объекта,
 * для определения необходимости создания нового экземпляра ViewModel
 * @lazyCreateViewModel - Функция вызывается если ViewModel не найден в пуле и
 * нужно создать новый объект
 */
inline fun <reified T : ViewModel> ViewModelReused.addEx(vararg objectList: Any, crossinline lazyCreateViewModel: (Int) -> T) {
	add(T::class.java, { lazyCreateViewModel(it) }, objectList)
}

//inline fun <reified T : ViewModel> ViewModelReused.addEx(position: Int, vararg objectList: Any, crossinline func: (Int) -> ViewModel) {
//	add(position, T::class.java, { func.invoke(it) }, objectList)
//}
