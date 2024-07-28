package com.project.shopapp.dtos;

import lombok.*;

@Data
@NoArgsConstructor
public class ThongKeThangDTO extends ThongKeDTO {
    private String month;
    private int year;

    @Builder
    public ThongKeThangDTO(String month,  int year, double totalMoney, Integer numberOfProducts) {
        super(totalMoney, numberOfProducts);
        this.month = month;
        this.year = year;
    }
}

