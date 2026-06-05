package com.example.demo.dto;

import java.util.List;

public class ServiceInfoRequest {

    private String trademarkName;
    private String serviceDescription;
    private List<Integer> selectedNiceClasses;

    public ServiceInfoRequest() {
    }

    public String getTrademarkName() {
        return trademarkName;
    }

    public void setTrademarkName(String trademarkName) {
        this.trademarkName = trademarkName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public List<Integer> getSelectedNiceClasses() {
        return selectedNiceClasses;
    }

    public void setSelectedNiceClasses(List<Integer> selectedNiceClasses) {
        this.selectedNiceClasses = selectedNiceClasses;
    }
}