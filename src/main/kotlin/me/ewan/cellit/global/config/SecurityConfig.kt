package me.ewan.cellit.global.config

import me.ewan.cellit.domain.account.handler.LoginFailureHandler
import me.ewan.cellit.domain.account.service.AccountService
import me.ewan.cellit.global.security.HeaderTokenExtractor
import me.ewan.cellit.global.security.JwtDecoder
import me.ewan.cellit.global.security.filters.FormLoginFilter
import me.ewan.cellit.global.security.filters.JwtAuthorizationFilter
import me.ewan.cellit.global.security.handlers.FormLoginAuthenticationFailureHandler
import me.ewan.cellit.global.security.handlers.FormLoginAuthenticationSuccessHandler
import me.ewan.cellit.global.security.providers.FormLoginAuthenticationProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.session.HttpSessionEventPublisher

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(1)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var provider: FormLoginAuthenticationProvider

    @Autowired
    private lateinit var formLoginAuthenticationSuccessHandler: FormLoginAuthenticationSuccessHandler

    @Autowired
    private lateinit var formLoginAuthenticationFailureHandler: FormLoginAuthenticationFailureHandler

    @Autowired
    private lateinit var extractor: HeaderTokenExtractor

    @Autowired
    private lateinit var decoder: JwtDecoder

    @Throws(Exception::class)
    protected fun formLoginFilter(): FormLoginFilter {
        val filter = FormLoginFilter("/formlogin", formLoginAuthenticationSuccessHandler, formLoginAuthenticationFailureHandler)
        filter.setAuthenticationManager(authenticationManagerBean())
        return filter
    }

//    @Throws(Exception::class)
//    protected fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
//        val filter = JwtAuthenticationFilter("/formlogin", formLoginAuthenticationSuccessHandler, formLoginAuthenticationFailureHandler)
//        filter.setAuthenticationManager(authenticationManagerBean())
//        return filter
//    }

    @Bean
    fun tokenStore(): TokenStore = InMemoryTokenStore()

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun authenticationFailureHandler(): AuthenticationFailureHandler = LoginFailureHandler()

    @Bean
    fun sessionRegistry(): SessionRegistry = SessionRegistryImpl()

    @Bean
    fun httpSessionEventPublisher(): ServletListenerRegistrationBean<HttpSessionEventPublisher> = ServletListenerRegistrationBean(HttpSessionEventPublisher())

    override fun configure(auth: AuthenticationManagerBuilder?) {

        auth?.authenticationProvider(this.provider)

        auth?.userDetailsService(accountService)
                ?.passwordEncoder(passwordEncoder)
    }

    @Throws(Exception::class)
    override fun configure(web: WebSecurity?) {
        web?.let {
            //it.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations())
            it.ignoring()
                    .antMatchers("/assets/**")
                    .antMatchers("/dist/**")
                    .antMatchers("/images/**")
        }
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {

        http.csrf().disable()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.headers().frameOptions().disable()
        http.addFilterBefore(formLoginFilter(), UsernamePasswordAuthenticationFilter::class.java)
        http
                .addFilter(JwtAuthorizationFilter(authenticationManager(), extractor, decoder))
                .authorizeRequests()
                .mvcMatchers("/signUp", "/login**", "/loginError", "/formlogin").permitAll()
                //.antMatchers(HttpMethod.GET, "/api/**").permitAll()
                .mvcMatchers("/admin").hasAnyAuthority("ROLE_USER")
                .anyRequest().hasRole("USER")



//        http?.let {
//            it.authorizeRequests()
//                    .mvcMatchers("/signUp", "/login**", "/loginError", "/formlogin").permitAll()
//                    .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
//                    .mvcMatchers("/admin").hasRole("ADMIN")
//                    .anyRequest().authenticated()
//                    .and()
//                    .exceptionHandling()
//                    .accessDeniedHandler(OAuth2AccessDeniedHandler())
//
//            it.formLogin()
//                    .loginPage("/login")
//                    .failureHandler(authenticationFailureHandler())
//                    .permitAll()
//
//            it.httpBasic()
//
//            it.logout()
//                    .logoutUrl("/logout")
//                    .logoutSuccessUrl("/")
//                    .deleteCookies("JSESSIONID")
//                    .invalidateHttpSession(true)

//            it.sessionManagement()
//                    .sessionFixation()
//                    .changeSessionId()
//                    .invalidSessionUrl("/login")
//                    .maximumSessions(1)
//                    .maxSessionsPreventsLogin(true)
//
//            it.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

    }
}