package me.ewan.cellit.domain.account.service

import me.ewan.cellit.domain.account.domain.Account
import me.ewan.cellit.domain.account.dao.AccountRepository
import me.ewan.cellit.domain.account.domain.AccountRole
import me.ewan.cellit.domain.cell.model.CellDto
import me.ewan.cellit.global.security.AccountContext
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AccountService : UserDetailsService {

    companion object : KLogging()

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var passWordEncoder: PasswordEncoder

    override fun loadUserByUsername(username: String?): UserDetails {
        val account = accountRepository.findByAccountname(username)
        //TODO NoSuchElementException

//        return User.builder()
//                .username(account.accountname)
//                .password(account.password)
//                .roles(account.role.toString())
//                .build()

        return getAccountContext(account)
    }

    private fun getAccountContext(account: Account): AccountContext = AccountContext.fromAccountModel(account)


    fun createAccount(account: Account) : Account {
        account.password = passWordEncoder.encode(account.password)
        val savedAccount = accountRepository.save(account)
        return savedAccount
    }

    fun getAccount(accountId: Long): Account = accountRepository.findByAccountId(accountId)
    fun getAccount(username: String): Account = accountRepository.findByAccountname(username)

    fun getCellsWithAccountId (accountId: Long): List<CellDto> {
        val account = accountRepository.findAccountFetch(accountId)

        val cellDtos = account.accountCells.map {
            CellDto(cellId = it.cell.cellId, cellName = it.cell.cellName)
        }
        return cellDtos
    }

}