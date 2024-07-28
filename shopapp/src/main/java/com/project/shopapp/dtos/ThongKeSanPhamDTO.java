package com.project.shopapp.dtos;

import lombok.*;

@Data
@NoArgsConstructor
public class ThongKeSanPhamDTO extends ThongKeDTO {
    private String productName;

    @Builder
    public ThongKeSanPhamDTO(String productName, double totalMoney, Integer numberOfProducts) {
        super(totalMoney, numberOfProducts);
        this.productName = productName;
    }
}

