package run.mycode.ltioauthdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import run.mycode.basiclti.security.LtiAuthenticationProcessingFilter;
import run.mycode.basiclti.service.LtiKeyService;
import run.mycode.basiclti.service.NonceService;
import run.mycode.basiclti.service.SimpleNonceServiceImpl;

@Configuration
@Order(1)
public class LtiSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private LtiKeyService keyService;
    
    @Autowired
    private NonceService nonceService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
                .ignoringAntMatchers("/lti/**") // LTI launches are made using HTTP POST, but won't have csrf tokens so must be ignored -- nonce should eliminate csrf
            .and()
            .antMatcher("/lti/**")
                .headers().frameOptions().disable() // Allow launches in iframes
            .and()
                .addFilterBefore(new LtiAuthenticationProcessingFilter(keyService, nonceService),
                        UsernamePasswordAuthenticationFilter.class) // Authenticate LTI launches before requiring username/password
            .authorizeRequests()
                .antMatchers("/lti/**") // Launches will be made on any of the resources below lti
                .authenticated()        // and must be authenticated (by the filter)
                ;
    }
    
    @Bean(name = "nonceService")
    public NonceService createNonceService() {
        return new SimpleNonceServiceImpl(600);
    }
    
//    @Bean(name = "keyService")
//    public LtiKeyService createKeyService() {
//        return new MockKeyService();
//    }
    
}