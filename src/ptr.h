/*
 * Object-Oriented Programming
 * Copyright (C) 2012 Robert Grimm
 * Modifications copyright (C) 2013 Thomas Wies
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */

#pragma once

#include <cstring>
#include <iostream>

#if 0
#define TRACE(s) \
  std::cout << __FUNCTION__ << ":" << __LINE__ << ":" << s << std::endl
#else
#define TRACE(s)
#endif

namespace __rt {

  template<typename T>
  struct object_policy {
    static void destroy(T* addr) {
      delete addr;
    }
  };

  template<typename T>
  struct array_policy {
    static void destroy(T* addr) {
      delete[] addr;
    }
  };

  template<typename T>
  struct java_policy {
    static void destroy(T* addr) {
      if (0 != addr) addr->__vptr->__delete(addr);
    }
  };

  template<typename T, template <typename> class P = java_policy>
  class Ptr {
    T* addr;
    size_t* counter;

  public:
    typedef T value_type;
    typedef P<T> policy_type;

    Ptr(T* addr = 0) : addr(addr), counter(new size_t(1)) {
      TRACE(addr);
    }

    Ptr(const Ptr& other) : addr(other.addr), counter(other.counter) {
      TRACE(addr);
      ++(*counter);
    }

    ~Ptr() {
      TRACE(addr);
      if (0 == --(*counter)) {
        policy_type::destroy(addr);;
        delete counter;
      }
    }

    Ptr& operator=(const Ptr& right) {
      TRACE(addr);
      if (addr != right.addr) {
        if (0 == --(*counter)) {
          policy_type::destroy(addr);
          delete counter;
        }
        addr = right.addr;
        counter = right.counter;
        ++(*counter);
      }
      return *this;
    }

    T& operator*()  const { TRACE(addr); return *addr; }
    T* operator->() const { TRACE(addr); return addr;  }
    T* raw()        const { TRACE(addr); return addr;  }

    template<typename U, template <typename> class Q>
    friend class Ptr;

    template<typename U, template <typename> class Q>
    Ptr(const Ptr<U,Q>& other) : addr((T*)other.addr), counter(other.counter) {
      TRACE(addr);
      ++(*counter);
    }

    template<typename U, template <typename> class Q>
    bool operator==(const Ptr<U,Q>& other) const {
      return addr == (T*)other.addr;
    }
    
    template<typename U, template <typename> class Q>
    bool operator!=(const Ptr<U,Q>& other) const {
      return addr != (T*)other.addr;
    }

  };

}
