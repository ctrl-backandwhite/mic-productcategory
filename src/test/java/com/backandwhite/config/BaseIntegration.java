package com.backandwhite.config;

import com.backandwhite.common.constants.AppConstants;
import com.backandwhite.core.test.JwtTestUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(TestContainersConfiguration.class)
public abstract class BaseIntegration extends com.backandwhite.core.test.BaseIntegration {

    protected static final String NX_HEADER = AppConstants.HEADER_NX036_AUTH;
    protected static final String NX_VALUE = "test";

    @Autowired
    private JwtTestUtil jwtTestUtil;

    public String getToken(List<String> roles) {
        return jwtTestUtil.getToken("John Doe", roles);
    }
}
