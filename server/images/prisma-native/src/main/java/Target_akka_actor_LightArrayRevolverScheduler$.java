import akka.actor.LightArrayRevolverScheduler$;
import com.oracle.svm.core.annotate.*;
import com.oracle.svm.core.annotate.RecomputeFieldValue.*;

//@TargetClass(className = "akka.actor.LightArrayRevolverScheduler$")
@TargetClass(value = LightArrayRevolverScheduler$.class)
final class Target_akka_actor_LightArrayRevolverScheduler$ {
    @Alias @RecomputeFieldValue(kind = Kind.FieldOffset, //
            declClassName = "akka.actor.LightArrayRevolverScheduler$TaskHolder", //
            name = "task")
    public final long akka$actor$LightArrayRevolverScheduler$$taskOffset = 0L;
}