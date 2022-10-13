package com.syncloudsoft.mobserve.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ForwardingRuleDao {

    @Delete
    fun delete(forwardingRule: ForwardingRule)

    @Query("DELETE FROM forwarding_rules")
    fun deleteAll()

    @Query("SELECT * FROM forwarding_rules WHERE _id = :id LIMIT 1")
    fun get(id: Int): ForwardingRule

    @Query("SELECT * FROM forwarding_rules ORDER BY name ASC")
    fun getAll(): List<ForwardingRule>

    @Query("SELECT * FROM forwarding_rules ORDER BY name ASC")
    fun getAllAsLiveData(): LiveData<List<ForwardingRule>>

    @Insert
    fun insertAll(vararg forwardingRules: ForwardingRule)

    @Update
    fun update(forwardingRule: ForwardingRule)
}
