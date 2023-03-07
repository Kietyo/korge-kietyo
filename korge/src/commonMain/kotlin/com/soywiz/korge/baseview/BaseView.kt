package com.soywiz.korge.baseview

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korev.*
import com.soywiz.korge.component.*
import com.soywiz.korge.view.*
import com.soywiz.korio.lang.*
import kotlin.jvm.*

interface InvalidateNotifier {
    fun invalidatedView(view: BaseView?)
}

//open class BaseView : BaseEventListener() {
open class BaseView : BaseEventListener() {
    val baseParent: BaseView? get() = eventListenerParent as? BaseView?

    open fun invalidateRender() {
    }

    @PublishedApi internal var __components: EventListenerFastMap<ComponentType<out Component>, FastArrayList<Component>>? = null
    @PublishedApi internal val __componentsSure: EventListenerFastMap<ComponentType<out Component>, FastArrayList<Component>> get() {
        if (__components == null) __components = EventListenerFastMap()
        return __components!!
    }

    fun __updateChildListenerCount(view: BaseView, add: Boolean) {
        view.__components?.forEach { key, value, count ->
            deltaComponent(key, if (add) +count else -count)
        }
    }

    fun getComponentCountInDescendants(clazz: ComponentType<out Component>): Int {
        return __components?.getCount(clazz) ?: 0
    }

    private fun deltaComponent(clazz: ComponentType<out Component>, delta: Int) {
        if (delta == 0) return
        __componentsSure.setCount(clazz, __componentsSure.getCount(clazz) + delta)
        baseParent?.deltaComponent(clazz, delta)
    }

    fun <T : Component> getComponentsOfType(type: ComponentType<T>): FastArrayList<T>? {
        return __components?.getValue(type) as FastArrayList<T>?
    }

    fun <T : Component> forEachComponentOfTypeRecursive(type: ComponentType<T>, temp: FastArrayList<Component> = FastArrayList(), results: EventResult? = null, block: (T) -> Unit) {
        temp as FastArrayList<T>
        temp.clear()
        try {
            getComponentOfTypeRecursive(type, temp, results)
            temp.fastForEach(block)
        } finally {
            temp.clear()
        }
    }

    fun <T : Component> getComponentOfTypeRecursive(type: ComponentType<T>, out: FastArrayList<T>, results: EventResult? = null) {
        if (getComponentCountInDescendants(type) <= 0) return

        getComponentOfTypeRecursiveChildren(type, out, results)
        val components = getComponentsOfType(type)
        components?.let { out.addAll(it) }
        results?.let {
            it.iterationCount++
            it.resultCount += components?.size ?: 0
        }
    }

    protected open fun <T : Component> getComponentOfTypeRecursiveChildren(type: ComponentType<T>, out: FastArrayList<T>, results: EventResult?) {
    }

    fun <T : TypedComponent<T>> getFirstComponentOfType(type: ComponentType<T>): T? {
        return getComponentsOfType(type)?.firstOrNull()
    }

    /** Adds a [component] to this view */
    fun <T : Component> addComponent(component: T): T {
        val type = component.type
        __componentsSure.getOrPutValue(type) { FastArrayList() }.add(component)
        deltaComponent(type, +1)
        return component
    }

    /** Removes a specific [component] from the view */
    fun removeComponent(component: Component) {
        if (__components?.getValue(component.type)?.remove(component) == true) {
            deltaComponent(component.type, -1)
        }
    }

    /** Checks if a [component] is part of a view */
    fun hasComponent(component: Component): Boolean {
        return (__components?.getValue(component.type)?.contains(component) == true)
    }

    /** Removes all the components attached to this view of component [clazz] */
    fun removeAllComponentsOfType(type: ComponentType<*>) {
        val components = getComponentsOfType(type)?.toList() ?: return
        components.fastForEach { it.removeFromView() }
    }

    /** Removes all the components attached to this view */
    fun removeAllComponents() {
        __components?.forEach { key, _, _ ->
            removeAllComponentsOfType(key)
        }
    }

    /** Creates a typed [T] component (using the [gen] factory function) if the [View] doesn't have any of that kind, or returns a component of that type if already attached */
    inline fun <T : TypedComponent<T>> getOrCreateComponent(type: ComponentType<T>, gen: (BaseView) -> T): T {
        return getComponentsOfType(type)?.firstOrNull()
            ?: addComponent(gen(this))
    }

    inline fun <T : TypedComponent<T>, reified TR : T> getOrCreateComponentTyped(type: ComponentType<T>, gen: (BaseView) -> TR): TR {
        return (getComponentsOfType(type)?.firstOrNull { it is TR } as? TR?)
            ?: (addComponent(gen(this)) as TR)
    }

    inline fun <reified T : TouchComponent> getOrCreateComponentTouch(gen: (BaseView) -> T): T = getOrCreateComponentTyped(TouchComponent, gen)
}
