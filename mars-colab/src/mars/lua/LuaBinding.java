package mars.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;

import mars.Globals;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

public class LuaBinding extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue lib = tableOf();
		env.set("mars", lib);
		lib.set("getgpr", new LuaFunc_getgpr());
		lib.set("setgpr", new LuaFunc_setgpr());
		lib.set("loadb", new LuaFunc_loadb());
		lib.set("loadh", new LuaFunc_loadh());
		lib.set("loadw", new LuaFunc_loadw());
		lib.set("storeb", new LuaFunc_storeb());
		lib.set("storeh", new LuaFunc_storeh());
		lib.set("storew", new LuaFunc_storew());
		return lib;
	}

	// register functions
	static class LuaFunc_getgpr extends OneArgFunction {
		public LuaValue call(LuaValue lvNum) {
			int num = lvNum.checkint();
			int val = RegisterFile.getValue(num);
			return LuaValue.valueOf(val);
		}
	}

	static class LuaFunc_setgpr extends TwoArgFunction {
		public LuaValue call(LuaValue lvNum, LuaValue lvVal) {
			int num = lvNum.checkint();
			int val = lvVal.checkint();
			RegisterFile.updateRegister(num, val);
			return LuaValue.NIL;
		}
	}

	// load functions
	static class LuaFunc_loadb extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getByte(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadb(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	static class LuaFunc_loadh extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getHalf(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadh(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	static class LuaFunc_loadw extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getWord(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadw(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	// store functions
	static class LuaFunc_storeb extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setByte(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storeb(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_storeh extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setHalf(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storeh(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_storew extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setWord(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storew(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

}
