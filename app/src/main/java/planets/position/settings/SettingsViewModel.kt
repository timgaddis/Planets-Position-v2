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

package planets.position.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private var raIndex = MutableLiveData<Int>()
    private var decIndex = MutableLiveData<Int>()
    private var azIndex = MutableLiveData<Int>()
    private var altIndex = MutableLiveData<Int>()

    fun setRAIndex(i: Int) {
        raIndex.value = i
    }

    fun setDecIndex(i: Int) {
        decIndex.value = i
    }

    fun setAzIndex(i: Int) {
        azIndex.value = i
    }

    fun setAltIndex(i: Int) {
        altIndex.value = i
    }

    fun getRAIndex(): LiveData<Int> {
        return raIndex
    }

    fun getDecIndex(): LiveData<Int> {
        return decIndex
    }

    fun getAzIndex(): LiveData<Int> {
        return azIndex
    }

    fun getAltIndex(): LiveData<Int> {
        return altIndex
    }
}
