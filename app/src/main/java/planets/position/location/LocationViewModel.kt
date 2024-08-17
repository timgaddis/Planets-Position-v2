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
import planets.position.database.timezone.TimeZone
import planets.position.database.timezone.TimeZoneRepository
import planets.position.database.timezone.WorldCity

class LocationViewModel(
    private val timeZoneRepository: TimeZoneRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var city: LiveData<WorldCity>? = null
    private var zone: LiveData<TimeZone>? = null
    private var zoneName: String = ""

    init {
        city = savedStateHandle.getLiveData("cityID", 0).switchMap { x ->
            timeZoneRepository.getCityData(x)
        }
        zone = savedStateHandle.getLiveData("locTime", 0L).switchMap { x ->
            timeZoneRepository.getZoneOffset(zoneName, x)
        }
    }

    fun saveCityID(id: Int) {
        savedStateHandle["cityID"] = id
    }

    fun getCity(): LiveData<WorldCity>? {
        return city
    }

    fun saveZone(name: String, time: Long) {
        zoneName = name
        savedStateHandle["locTime"] = time
    }

    fun getZone(): LiveData<TimeZone>? {
        return zone
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

                return LocationViewModel(
                    (application as PlanetApplication).timeZoneRepository,
                    savedStateHandle
                ) as T
            }
        }
    }
}
