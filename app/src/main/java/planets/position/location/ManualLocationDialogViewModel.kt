/*
 * Planet's Position
 * A program to calculate the position of the planets in the night sky
 * based on a given location on Earth.
 * Copyright (c) 2023-2024 Tim Gaddis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package planets.position.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewmodel.CreationExtras
import planets.position.PlanetApplication
import planets.position.database.timezone.Country
import planets.position.database.timezone.State
import planets.position.database.timezone.TimeZoneRepository
import planets.position.database.timezone.WorldCity

class ManualLocationDialogViewModel(
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var country: LiveData<Country>? = null
    private var stateList: LiveData<List<State>>? = null
    private var cityList: LiveData<List<WorldCity>>? = null
    private var cityID: Int = 0

    init {
        country = savedStateHandle.getLiveData("country", "").switchMap { i ->
            timeZoneRepository.getCounrty(i)
        }
        stateList = savedStateHandle.getLiveData("cc", "").switchMap { i ->
            timeZoneRepository.getStateList(i)
        }
        cityList = savedStateHandle.getLiveData("state", "").switchMap { i ->
            timeZoneRepository.getCityList(i)
        }
    }

    fun saveState(state: String) {
        savedStateHandle["state"] = state
    }

    fun getCityList(): LiveData<List<WorldCity>>? {
        return cityList
    }

    fun saveCountryCode(cc: String) {
        savedStateHandle["cc"] = cc
    }

    fun getStateList(): LiveData<List<State>>? {
        return stateList
    }

    fun saveCountry(country: String) {
        savedStateHandle["country"] = country
    }

    fun getCountry(): LiveData<Country>? {
        return country
    }

    fun saveCityID(c: Int) {
        cityID = c
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()

                return ManualLocationDialogViewModel(
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
