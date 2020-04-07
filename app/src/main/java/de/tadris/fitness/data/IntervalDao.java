/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface IntervalDao {

    @Query("SELECT * FROM interval_queue WHERE id = :id")
    IntervalQueue getQueue(long id);

    @Query("SELECT * FROM interval WHERE queue_id = :queueId")
    Interval[] getAllIntervalsOfQueue(long queueId);

    @Query("SELECT * FROM interval_queue where state = 0")
    IntervalQueue[] getVisibleQueues();

    @Insert
    void insertIntervalQueue(IntervalQueue queue);

    @Insert
    void insertInterval(Interval interval);

    @Delete
    void deleteIntervalQueue(IntervalQueue queue);

    @Delete
    void deleteInterval(Interval interval);

}
