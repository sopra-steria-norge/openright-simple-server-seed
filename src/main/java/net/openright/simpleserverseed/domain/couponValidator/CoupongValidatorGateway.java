package net.openright.simpleserverseed.domain.couponValidator;

import net.openright.infrastructure.util.ExceptionUtil;
import net.openright.infrastructure.util.IOUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CoupongValidatorGateway {

    public boolean validate(String coupong){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8080/?coupon="+coupong).openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpServletResponse.SC_OK) {
                return Boolean.parseBoolean(IOUtil.toString(con.getInputStream()));
            }
            throw new IllegalStateException("Could not validate coupon " + coupong + " due to some error");
        } catch (IOException e) {
            throw ExceptionUtil.soften(e);
        }
    }
}
