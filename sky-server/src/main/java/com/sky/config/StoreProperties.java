package com.sky.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "store")
public class StoreProperties {
    
    private String address;
    
    private Integer maxDeliveryDistance = 5000;
}
