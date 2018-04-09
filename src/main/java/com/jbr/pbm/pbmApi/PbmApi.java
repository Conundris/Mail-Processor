package com.jbr.pbm.pbmApi;

import com.jbr.pbm.mail.MailUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "pbm")
@EnableConfigurationProperties
public class PbmApi {

    @Value("${pbm.mail.api.link}")
    private String pbmApiLink;

    public PbmApi() {}

    public String getPbmApiLink() {
        return pbmApiLink;
    }
    public boolean isValidCustomerNumber(List<String> customerNumbers) {
        URL url;
        for (String customerNumber: customerNumbers) {
            try {
                url = new URL(pbmApiLink + "/customer/customerNumber/" + customerNumber);
                String response = MailUtils.validateCustomer(url);
                if (response.equals("[]"))
                    return false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public String getCustomerDetailsByEmail(String payload) {
        URL url = null;
        try {
            url = new URL( pbmApiLink + "/customer/email/" + payload);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return MailUtils.validateCustomer(url);
    }

}
