import os
import datetime as dt
from sgp4.api import jday
from sgp4.api import Satrec
from sgp4.api import SGP4_ERRORS
import pandas as pd

import spacetrack.operators as op
from spacetrack import SpaceTrackClient

from astropy.time import Time
from astropy.coordinates import TEME, CartesianDifferential, CartesianRepresentation
from astropy import units as u
from astropy.coordinates import ITRS

st = SpaceTrackClient(identity='hitalo.c.a@gmail.com', password='EuMeChamoHitalo2000!')

data = st.tle_latest(iter_lines=True, ordinal=1, epoch='>now-30',
                     mean_motion=op.inclusive_range(2, 5),
                     eccentricity=op.less_than(0.01), format='tle')

with open('tle_latest.txt', 'w') as fp:
    for line in data:
        fp.write(line + '\n')

tle_latest = pd.read_csv("tle_latest.txt", header=None)

s_array = tle_latest[tle_latest.index%2 == 0].values.tolist()
t_array = tle_latest[tle_latest.index%2 != 0].values.tolist()
timestamp_now = dt.datetime.now()

jd, fr = jday(timestamp_now.year, timestamp_now.month, timestamp_now.day, timestamp_now.hour, timestamp_now.minute, timestamp_now.second)

e, r, v = [], [], []
lat, lon, hgt = [], [], []
print('Converting tle data...')

indexfile = open("/var/www/html/index2.html", "w")

for i in range(len(s_array)):
  print(i)
  satellite = Satrec.twoline2rv(s_array[i][0], t_array[i][0])
  e_, r_, v_ = satellite.sgp4(jd, fr)
  if e_ != 0:
    print(f'Error: {SGP4_ERRORS[e_]}')
  else:
    e.append(e_)
    r.append(r_)
    v.append(v_)

    t = Time(jd, format='jd')
    #print(t)

    teme_p = r_
    teme_v = v_

    #from astropy.coordinates import TEME, CartesianDifferential, CartesianRepresentation
    #from astropy import units as u
    teme_p = CartesianRepresentation(teme_p*u.km)
    teme_v = CartesianDifferential(teme_v*u.km/u.s)
    teme = TEME(teme_p.with_differentials(teme_v), obstime=t)

    #from astropy.coordinates import ITRS
    itrs = teme.transform_to(ITRS(obstime=t))  
    location = itrs.earth_location
    #location.geodetic
    
    lon_ = location.geodetic.lon.deg
    lat_ = location.geodetic.lat.deg
    hgt_ = location.geodetic.height.value

    lon.append(lon_)
    lat.append(lat_)
    hgt.append(hgt_)

    
    indexfile.write(str(lon_) + "," + str(lat_) + "," + str(hgt_) + "\n")

indexfile.close()
os.replace("/var/www/html/index2.html", "/var/www/html/index.html")
print('Done!')
