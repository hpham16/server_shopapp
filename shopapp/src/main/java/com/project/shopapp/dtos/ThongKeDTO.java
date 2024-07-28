package com.project.shopapp.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDTO {
    private double totalMoney;
    private Integer numberOfProducts; // sử dụng Integer để có thể nhận giá trị null
}