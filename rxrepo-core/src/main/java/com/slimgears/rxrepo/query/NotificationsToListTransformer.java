package com.slimgears.rxrepo.query;

import com.google.common.collect.ImmutableList;
import com.slimgears.rxrepo.query.provider.SortingInfo;
import com.slimgears.rxrepo.query.provider.SortingInfos;
import com.slimgears.util.autovalue.annotations.MetaClassWithKey;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

public class NotificationsToListTransformer<K, T> implements ObservableTransformer<List<Notification<T>>, List<T>> {
    private final static Logger log = LoggerFactory.getLogger(NotificationsToListTransformer.class);
    private final @Nullable Long limit;
    private final Map<K, T> map = new HashMap<>();
    private final Set<T> set;
    private final MetaClassWithKey<K, T> metaClass;

    private NotificationsToListTransformer(MetaClassWithKey<K, T> metaClass,
                                           ImmutableList<SortingInfo<T, ?, ? extends Comparable<?>>> sortingInfos,
                                           @Nullable Long limit) {
        log.trace("Creating instance of list transformer for {}", metaClass.simpleName());
        this.metaClass = metaClass;
        this.limit = limit;
        this.set = Collections.synchronizedSet(Optional
                .ofNullable(SortingInfos.toComparator(sortingInfos))
                .<Set<T>>map(TreeSet::new)
                .orElseGet(LinkedHashSet::new));
    }

    public static <K, T> NotificationsToListTransformer<K, T> create(
            MetaClassWithKey<K, T> metaClass,
            ImmutableList<SortingInfo<T, ?, ? extends Comparable<?>>> sortingInfos,
            @Nullable Long limit) {
        return new NotificationsToListTransformer<>(metaClass, sortingInfos, limit);
    }

    @Override
    public Observable<List<T>> apply(Observable<List<Notification<T>>> src) {
        return src
                .doOnNext(this::updateMap)
                .<List<T>>map(n -> toList())
                .doOnNext(l -> log.trace("List update: {} items", l.size()));
    }

    private ImmutableList<T> toList() {
        return Optional.ofNullable(limit)
                .map(l -> set.stream().limit(l).collect(ImmutableList.toImmutableList()))
                .orElseGet(() -> ImmutableList.copyOf(set));
    }

    private boolean updateMap(List<Notification<T>> notifications) {
        return notifications.stream().map(this::onNotification)
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    private synchronized boolean onNotification(Notification<T> notification) {
        if (notification.isDelete()) {
            return Optional.ofNullable(notification.oldValue())
                    .map(val -> metaClass.keyProperty().getValue(val))
                    .map(map::remove)
                    .map(set::remove)
                    .orElse(false);
        } else {
            T value = notification.newValue();
            return Optional.ofNullable(value)
                    .map(metaClass::keyOf)
                    .map(key -> {
                        Optional.ofNullable(map.put(key, value))
                                .ifPresent(set::remove);
                        return set.add(value);
                    })
                    .orElse(false);
        }
    }
}
