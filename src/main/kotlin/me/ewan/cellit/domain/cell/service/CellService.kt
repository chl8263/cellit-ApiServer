package me.ewan.cellit.domain.cell.service

import me.ewan.cellit.domain.account.service.AccountService
import me.ewan.cellit.domain.cell.dao.AccountCellRepository
import me.ewan.cellit.domain.cell.dao.CellRepository
import me.ewan.cellit.domain.cell.vo.domain.AccountCell
import me.ewan.cellit.domain.cell.vo.domain.Cell
import me.ewan.cellit.domain.cell.vo.model.AccountCellRole
import me.ewan.cellit.domain.cell.vo.dto.CellDto
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CellService {
    @Autowired
    lateinit var cellRepository: CellRepository

    @Autowired
    lateinit var accountService: AccountService

    @Autowired
    lateinit var accountCellRepository: AccountCellRepository

    @Autowired
    lateinit var modelMapper: ModelMapper

    fun createCell(cellDto: CellDto, name: String): CellDto {

        val currentUser = accountService.getAccount(name)

        val cell = modelMapper.map(cellDto, Cell::class.java)
        val savedCell = cellRepository.save(cell)
        val accountCell = AccountCell(accountCellRole = AccountCellRole.ADMIN, account = currentUser, cell = savedCell)

        val savedAccountCell = accountCellRepository.save(accountCell)

        return modelMapper.map(cell, CellDto::class.java)
    }
}