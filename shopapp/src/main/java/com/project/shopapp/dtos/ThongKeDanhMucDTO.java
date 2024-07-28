package com.project.shopapp.dtos;

import lombok.*;

@Data
@NoArgsConstructor
public class ThongKeDanhMucDTO extends ThongKeDTO {
    private String categoryName;

    @Builder
    public ThongKeDanhMucDTO(String categoryName, double totalMoney, Integer numberOfProducts) {
        super(totalMoney, numberOfProducts);
        this.categoryName = categoryName;
    }
}
