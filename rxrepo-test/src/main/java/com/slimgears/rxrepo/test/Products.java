package com.slimgears.rxrepo.test;

import org.mockito.internal.util.collections.Iterables;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Products {
    public static Iterable<Product> createMany(int count) {
        return createMany(0, count);
    }

    public static Iterable<Product> createMany(int startingId, int count) {
        final Product.Type[] productTypes = {
                ProductPrototype.Type.ConsumerElectronics,
                ProductPrototype.Type.ComputeHardware,
                ProductPrototype.Type.ComputerSoftware
        };

        List<Inventory> inventories = IntStream.range(0, Math.max(1, count / 10))
                .mapToObj(i -> Inventory
                        .builder()
                        .id(UniqueId.inventoryId(i))
                        .name("Inventory " + i)
                        .build())
                .collect(Collectors.toList());

        List<Vendor> vendors = Stream
                .concat(
                        IntStream.range(0, 3)
                                .mapToObj(i -> Vendor
                                        .builder()
                                        .id(UniqueId.vendorId(i))
                                        .name("Vendor " + i)
                                        .build()),
                        Stream.of((Vendor)null))
                .collect(Collectors.toList());

        Date startDate = Date.from(LocalDate
                .of(2000, 1, 1)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant());

        return IntStream.range(startingId, startingId + count)
                .mapToObj(i -> Product.builder()
                        .key(UniqueId.productId(i))
                        .name("Product " + i)
                        .type(productTypes[i % productTypes.length])
                        .inventory(inventories.get(i % inventories.size()))
                        .vendor(vendors.get(i % vendors.size()))
                        .price(100 + (i % 7)*(i % 11) + i % 13)
                        .productionDate(new Date(startDate.getTime() + TimeUnit.DAYS.toMillis(i)))
                        .build())
                .collect(Collectors.toList());
    }

    public static Product createOne() {
        return Iterables.firstOf(createMany(1));
    }

    public static Product createOne(int id) {
        return Iterables.firstOf(createMany(id, 1));
    }
}
